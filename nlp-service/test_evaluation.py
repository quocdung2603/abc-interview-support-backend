"""
Test script for NLP Service - Answer Evaluation

Run this after starting the NLP service:
    uvicorn app.main:app --reload --port 5000
"""

import requests
import json

# Configuration
NLP_SERVICE_URL = "http://localhost:5000"
TEST_TOKEN = "test-token-12345"

def test_evaluate_answer(question, correct_answer, user_answer, max_score=10):
    """Test the /evaluate-answer endpoint"""
    url = f"{NLP_SERVICE_URL}/evaluate-answer"
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {TEST_TOKEN}"
    }
    
    data = {
        "question": question,
        "correct_answer": correct_answer,
        "user_answer": user_answer,
        "max_score": max_score
    }
    
    print(f"\n{'='*80}")
    print(f"TEST: {question[:50]}...")
    print(f"{'='*80}")
    print(f"Correct Answer: {correct_answer[:100]}...")
    print(f"User Answer: {user_answer[:100]}...")
    print(f"\nSending request...")
    
    try:
        response = requests.post(url, headers=headers, json=data, timeout=30)
        response.raise_for_status()
        
        result = response.json()
        
        print(f"\n✅ RESULT:")
        print(f"   Score: {result['score']}/{result['max_score']} ({result['percentage']}%)")
        print(f"   Is Correct: {'✅ Yes' if result['is_correct'] else '❌ No'}")
        print(f"   Confidence: {result['confidence']}")
        print(f"\n📝 Feedback:")
        print(f"   {result['feedback']}")
        
        if result['strengths']:
            print(f"\n💪 Strengths:")
            for strength in result['strengths']:
                print(f"   + {strength}")
        
        if result['weaknesses']:
            print(f"\n⚠️  Weaknesses:")
            for weakness in result['weaknesses']:
                print(f"   - {weakness}")
        
        if result['suggestions']:
            print(f"\n💡 Suggestions:")
            for suggestion in result['suggestions']:
                print(f"   → {suggestion}")
        
        return result
        
    except requests.exceptions.RequestException as e:
        print(f"\n❌ ERROR: {e}")
        return None

def main():
    print("🤖 NLP Service - Answer Evaluation Tests")
    print("=" * 80)
    
    # Test 1: Perfect Answer
    test_evaluate_answer(
        question="What is 2 + 2?",
        correct_answer="4",
        user_answer="4",
        max_score=10
    )
    
    # Test 2: Technical Question - Good Answer
    test_evaluate_answer(
        question="What is dependency injection in Spring Framework?",
        correct_answer="Dependency Injection is a design pattern where objects receive their dependencies from external sources rather than creating them internally. In Spring, this is achieved through constructor injection, setter injection, or field injection using @Autowired annotation. The IoC container manages these dependencies.",
        user_answer="DI is when you pass dependencies to a class instead of creating them inside. Spring uses @Autowired to inject beans automatically.",
        max_score=10
    )
    
    # Test 3: Partial Answer
    test_evaluate_answer(
        question="Explain the SOLID principles in software design",
        correct_answer="SOLID stands for: Single Responsibility (class should have one reason to change), Open-Closed (open for extension, closed for modification), Liskov Substitution (subtypes must be substitutable), Interface Segregation (many specific interfaces better than one general), Dependency Inversion (depend on abstractions, not concretions).",
        user_answer="SOLID is about writing good code. Single Responsibility means one class does one thing.",
        max_score=10
    )
    
    # Test 4: Wrong Answer
    test_evaluate_answer(
        question="What is Java?",
        correct_answer="Java is a high-level, object-oriented programming language developed by Sun Microsystems (now Oracle). It follows the principle of 'write once, run anywhere' (WORA) and runs on the Java Virtual Machine (JVM).",
        user_answer="Java is a type of coffee from Indonesia.",
        max_score=10
    )
    
    # Test 5: Vietnamese Question
    test_evaluate_answer(
        question="REST API là gì?",
        correct_answer="REST (Representational State Transfer) là một kiến trúc phần mềm cho các dịch vụ web, sử dụng các phương thức HTTP (GET, POST, PUT, DELETE) để thực hiện các thao tác CRUD trên tài nguyên. REST API không lưu trạng thái (stateless) và sử dụng định dạng JSON hoặc XML.",
        user_answer="REST API dùng HTTP để làm việc với tài nguyên, không lưu state.",
        max_score=10
    )
    
    print(f"\n{'='*80}")
    print("✅ All tests completed!")
    print(f"{'='*80}\n")

if __name__ == "__main__":
    main()
