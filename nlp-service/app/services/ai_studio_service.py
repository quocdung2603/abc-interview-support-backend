import requests
import json
from typing import Dict, Any, Optional
import os
from dotenv import load_dotenv

load_dotenv()

class AIStudioService:
    """Service for integrating with Google AI Studio (Gemini) for answer validation and grading"""
    
    def __init__(self):
        # Use API key from environment or provided key
        self.api_key = os.getenv('GOOGLE_API_KEY', 'AIzaSyARkZIcLTr1n-zH2U38ztdFzxq1zwKED_c')
        self.base_url = "https://generativelanguage.googleapis.com/v1beta"
        self.model = "gemini-1.5-flash"  # Using Gemini 1.5 Flash for fast responses
        
    async def evaluate_answer(self, question: str, correct_answer: str, user_answer: str, max_score: int = 10) -> Dict[str, Any]:
        """
        Evaluate if user's answer is correct compared to the expected answer
        
        Args:
            question: The question text
            correct_answer: The correct/expected answer
            user_answer: The user's submitted answer
            max_score: Maximum possible score (default 10)
            
        Returns:
            Dict containing evaluation results with score, feedback, and analysis
        """
        try:
            prompt = self._build_evaluation_prompt(question, correct_answer, user_answer, max_score)
            
            response = await self._call_gemini_api(prompt)
            
            # Parse the response from Gemini
            parsed_result = self._parse_evaluation_response(response, max_score)
            
            return {
                "score": parsed_result.get("score", 0),
                "max_score": max_score,
                "percentage": round((parsed_result.get("score", 0) / max_score) * 100, 2),
                "is_correct": parsed_result.get("score", 0) >= (max_score * 0.6),  # 60% threshold
                "feedback": parsed_result.get("feedback", ""),
                "strengths": parsed_result.get("strengths", []),
                "weaknesses": parsed_result.get("weaknesses", []),
                "suggestions": parsed_result.get("suggestions", []),
                "confidence": parsed_result.get("confidence", 0.8)
            }
            
        except Exception as e:
            print(f"Error evaluating answer: {str(e)}")
            return {
                "score": 0,
                "max_score": max_score,
                "percentage": 0,
                "is_correct": False,
                "feedback": f"Unable to evaluate answer: {str(e)}",
                "strengths": [],
                "weaknesses": [],
                "suggestions": [],
                "confidence": 0.0
            }
        
    async def validate_answer(self, question: str, answer: str, expected_answer: Optional[str] = None) -> Dict[str, Any]:
        """
        Validate if an answer is correct using AI Studio
        
        Args:
            question: The question text
            answer: The user's answer
            expected_answer: The expected correct answer (optional)
            
        Returns:
            Dict containing validation results
        """
        try:
            prompt = self._build_validation_prompt(question, answer, expected_answer)
            
            response = await self._call_ai_studio(prompt)
            
            return {
                "is_correct": response.get("is_correct", False),
                "confidence": response.get("confidence", 0.0),
                "feedback": response.get("feedback", ""),
                "score": response.get("score", 0),
                "max_score": response.get("max_score", 10),
                "explanation": response.get("explanation", ""),
                "suggestions": response.get("suggestions", [])
            }
            
        except Exception as e:
            return {
                "is_correct": False,
                "confidence": 0.0,
                "feedback": f"Error validating answer: {str(e)}",
                "score": 0,
                "max_score": 10,
                "explanation": "",
                "suggestions": []
            }
    
    async def grade_essay(self, question: str, answer: str, max_score: int = 10) -> Dict[str, Any]:
        """
        Grade an essay answer using AI Studio
        
        Args:
            question: The question text
            answer: The user's answer
            max_score: Maximum possible score
            
        Returns:
            Dict containing grading results
        """
        try:
            prompt = self._build_grading_prompt(question, answer, max_score)
            
            response = await self._call_ai_studio(prompt)
            
            return {
                "score": min(response.get("score", 0), max_score),
                "max_score": max_score,
                "feedback": response.get("feedback", ""),
                "criteria_scores": response.get("criteria_scores", {}),
                "strengths": response.get("strengths", []),
                "weaknesses": response.get("weaknesses", []),
                "suggestions": response.get("suggestions", [])
            }
            
        except Exception as e:
            return {
                "score": 0,
                "max_score": max_score,
                "feedback": f"Error grading essay: {str(e)}",
                "criteria_scores": {},
                "strengths": [],
                "weaknesses": [],
                "suggestions": []
            }
    
    def _build_evaluation_prompt(self, question: str, correct_answer: str, user_answer: str, max_score: int) -> str:
        """Build prompt for answer evaluation"""
        return f"""You are an expert teacher evaluating a student's answer. Compare the student's answer against the correct answer and grade it.

**Question:**
{question}

**Correct Answer:**
{correct_answer}

**Student's Answer:**
{user_answer}

**Instructions:**
1. Grade the student's answer on a scale of 0 to {max_score}
2. Consider semantic meaning, not just exact wording
3. Give partial credit for partially correct answers
4. Be fair but strict in grading

**Provide your evaluation in EXACTLY this JSON format (no extra text):**
{{
    "score": <number between 0 and {max_score}>,
    "feedback": "<detailed explanation of the grade>",
    "strengths": ["<strength1>", "<strength2>"],
    "weaknesses": ["<weakness1>", "<weakness2>"],
    "suggestions": ["<suggestion1>", "<suggestion2>"],
    "confidence": <number between 0.0 and 1.0>
}}

**Grading Criteria:**
- 100% ({max_score}/{max_score}): Perfect answer, covers all key points accurately
- 80-99% ({int(max_score*0.8)}-{max_score-1}/{max_score}): Very good, minor issues only
- 60-79% ({int(max_score*0.6)}-{int(max_score*0.79)}/{max_score}): Good understanding, some gaps
- 40-59% ({int(max_score*0.4)}-{int(max_score*0.59)}/{max_score}): Partial understanding, significant gaps
- 20-39% ({int(max_score*0.2)}-{int(max_score*0.39)}/{max_score}): Minimal understanding
- 0-19% (0-{int(max_score*0.19)}/{max_score}): Incorrect or completely off-topic

Return ONLY the JSON, no additional text."""

    def _parse_evaluation_response(self, response: Dict[str, Any], max_score: int) -> Dict[str, Any]:
        """Parse and validate the evaluation response from Gemini"""
        try:
            # Ensure score is within bounds
            score = max(0, min(response.get("score", 0), max_score))
            
            return {
                "score": score,
                "feedback": response.get("feedback", "No feedback provided"),
                "strengths": response.get("strengths", []),
                "weaknesses": response.get("weaknesses", []),
                "suggestions": response.get("suggestions", []),
                "confidence": min(1.0, max(0.0, response.get("confidence", 0.8)))
            }
        except Exception as e:
            print(f"Error parsing response: {e}")
            return {
                "score": 0,
                "feedback": "Unable to parse evaluation",
                "strengths": [],
                "weaknesses": [],
                "suggestions": [],
                "confidence": 0.0
            }

    async def _call_gemini_api(self, prompt: str) -> Dict[str, Any]:
        """Call Google Gemini API (AI Studio)"""
        url = f"{self.base_url}/models/{self.model}:generateContent"
        
        headers = {
            "Content-Type": "application/json",
        }
        
        params = {
            "key": self.api_key
        }
        
        payload = {
            "contents": [{
                "parts": [{
                    "text": prompt
                }]
            }],
            "generationConfig": {
                "temperature": 0.2,  # Low temperature for more consistent grading
                "topK": 40,
                "topP": 0.95,
                "maxOutputTokens": 2048,
            }
        }
        
        try:
            response = requests.post(url, headers=headers, params=params, json=payload, timeout=30)
            response.raise_for_status()
            
            result = response.json()
            
            # Extract the generated text
            if "candidates" in result and len(result["candidates"]) > 0:
                generated_text = result["candidates"][0]["content"]["parts"][0]["text"]
                
                # Clean up response - remove markdown code blocks if present
                generated_text = generated_text.strip()
                if generated_text.startswith("```json"):
                    generated_text = generated_text[7:]
                if generated_text.startswith("```"):
                    generated_text = generated_text[3:]
                if generated_text.endswith("```"):
                    generated_text = generated_text[:-3]
                generated_text = generated_text.strip()
                
                # Try to parse as JSON
                try:
                    return json.loads(generated_text)
                except json.JSONDecodeError as e:
                    print(f"JSON decode error: {e}")
                    print(f"Generated text: {generated_text}")
                    # Fallback response
                    return {
                        "score": 0,
                        "feedback": generated_text,
                        "strengths": [],
                        "weaknesses": ["Unable to parse evaluation"],
                        "suggestions": [],
                        "confidence": 0.5
                    }
            else:
                raise Exception("No response from Gemini API")
                
        except requests.exceptions.RequestException as e:
            raise Exception(f"Gemini API error: {str(e)}")
        except Exception as e:
            raise Exception(f"Error processing Gemini response: {str(e)}")
        """Build prompt for answer validation"""
        base_prompt = f"""
You are an expert teacher grading a student's answer. Please evaluate the following:

Question: {question}

Student's Answer: {answer}
"""
        
        if expected_answer:
            base_prompt += f"\nExpected Answer: {expected_answer}"
        
        base_prompt += """

Please provide your evaluation in the following JSON format:
{
    "is_correct": true/false,
    "confidence": 0.0-1.0,
    "feedback": "Detailed feedback for the student",
    "score": 0-10,
    "max_score": 10,
    "explanation": "Explanation of why the answer is correct/incorrect",
    "suggestions": ["suggestion1", "suggestion2"]
}

Consider:
- Accuracy of the answer
- Completeness
- Understanding demonstrated
- Clarity of explanation
- Technical correctness (if applicable)
"""
        
        return base_prompt
    
    def _build_grading_prompt(self, question: str, answer: str, max_score: int) -> str:
        """Build prompt for essay grading"""
        return f"""
You are an expert teacher grading an essay answer. Please evaluate the following:

Question: {question}

Student's Answer: {answer}

Please grade this answer on a scale of 0 to {max_score} and provide detailed feedback.

Provide your evaluation in the following JSON format:
{{
    "score": 0-{max_score},
    "feedback": "Comprehensive feedback for the student",
    "criteria_scores": {{
        "content": 0-{max_score//4},
        "organization": 0-{max_score//4},
        "clarity": 0-{max_score//4},
        "technical_accuracy": 0-{max_score//4}
    }},
    "strengths": ["strength1", "strength2"],
    "weaknesses": ["weakness1", "weakness2"],
    "suggestions": ["suggestion1", "suggestion2"]
}}

Consider:
- Content accuracy and depth
- Organization and structure
- Clarity of expression
- Technical accuracy
- Originality of thought
- Use of examples and evidence
"""
    
    async def _call_ai_studio(self, prompt: str) -> Dict[str, Any]:
        """Call Google AI Studio API"""
        url = f"{self.base_url}/models/{self.model}:generateContent"
        
        headers = {
            "Content-Type": "application/json",
        }
        
        params = {
            "key": self.api_key
        }
        
        payload = {
            "contents": [{
                "parts": [{
                    "text": prompt
                }]
            }],
            "generationConfig": {
                "temperature": 0.1,
                "topK": 32,
                "topP": 1,
                "maxOutputTokens": 2048,
            }
        }
        
        try:
            response = requests.post(url, headers=headers, params=params, json=payload)
            response.raise_for_status()
            
            result = response.json()
            
            # Extract the generated text
            if "candidates" in result and len(result["candidates"]) > 0:
                generated_text = result["candidates"][0]["content"]["parts"][0]["text"]
                
                # Try to parse as JSON
                try:
                    return json.loads(generated_text)
                except json.JSONDecodeError:
                    # If not JSON, return a basic response
                    return {
                        "is_correct": "correct" in generated_text.lower(),
                        "confidence": 0.7,
                        "feedback": generated_text,
                        "score": 5,
                        "max_score": 10,
                        "explanation": generated_text,
                        "suggestions": []
                    }
            else:
                raise Exception("No response from AI Studio")
                
        except requests.exceptions.RequestException as e:
            raise Exception(f"AI Studio API error: {str(e)}")
        except Exception as e:
            raise Exception(f"Error processing AI Studio response: {str(e)}")
    
    async def check_plagiarism(self, text: str) -> Dict[str, Any]:
        """
        Check for potential plagiarism using AI Studio
        
        Args:
            text: Text to check for plagiarism
            
        Returns:
            Dict containing plagiarism check results
        """
        try:
            prompt = f"""
Analyze the following text for potential plagiarism or originality issues:

Text: {text}

Please provide your analysis in the following JSON format:
{{
    "is_original": true/false,
    "confidence": 0.0-1.0,
    "similarity_score": 0.0-1.0,
    "feedback": "Analysis of originality",
    "concerns": ["concern1", "concern2"],
    "suggestions": ["suggestion1", "suggestion2"]
}}

Consider:
- Uniqueness of content
- Potential copying from common sources
- Originality of ideas
- Proper attribution if needed
"""
            
            response = await self._call_ai_studio(prompt)
            
            return {
                "is_original": response.get("is_original", True),
                "confidence": response.get("confidence", 0.8),
                "similarity_score": response.get("similarity_score", 0.1),
                "feedback": response.get("feedback", ""),
                "concerns": response.get("concerns", []),
                "suggestions": response.get("suggestions", [])
            }
            
        except Exception as e:
            return {
                "is_original": True,
                "confidence": 0.0,
                "similarity_score": 0.0,
                "feedback": f"Error checking plagiarism: {str(e)}",
                "concerns": [],
                "suggestions": []
            }



