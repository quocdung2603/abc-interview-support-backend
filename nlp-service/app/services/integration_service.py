import httpx
import asyncio
from typing import List, Dict, Any, Optional
import os
import logging
from .similarity_service import SimilarityService
from .grading_service import GradingService
from .gateway_client import GatewayClient
from ..config import settings

logger = logging.getLogger(__name__)


class IntegrationService:
    def __init__(self):
        # Initialize gateway client for all service-to-service calls
        self.gateway_client = GatewayClient(
            gateway_url=settings.GATEWAY_URL,
            jwt_secret=settings.JWT_SECRET
        )
        self.similarity_service = SimilarityService()
        self.grading_service = GradingService()
        
        # Legacy URLs kept for backward compatibility (deprecated)
        self.question_service_url = os.getenv("QUESTION_SERVICE_URL", "http://question-service:8085")
        self.exam_service_url = os.getenv("EXAM_SERVICE_URL", "http://exam-service:8086")
        
    async def check_question_duplicates(self, question_text: str, exclude_id: Optional[int] = None) -> Dict[str, Any]:
        """Check if a question is duplicate of existing questions"""
        try:
            # Get all questions from Question Service via Gateway
            response = await self.gateway_client.get("/questions")
            
            if response.status_code != 200:
                logger.error(f"Failed to fetch questions: {response.status_code}")
                return {
                    "error": "Failed to fetch questions from Question Service",
                    "similar_questions": [],
                    "is_duplicate": False
                }
            
            questions = response.json()
                
                # Filter out the question being checked if exclude_id is provided
                if exclude_id:
                    questions = [q for q in questions if q.get('id') != exclude_id]
                
                # Check for similar questions
                similar_questions = []
                for question in questions:
                    similarity = await self.similarity_service.calculate_similarity(
                        question_text, question.get('content', '')
                    )
                    
                    if similarity > 0.7:  # Threshold for similarity
                        similar_questions.append({
                            "question_id": question.get('id'),
                            "question_content": question.get('content'),
                            "similarity_score": similarity
                        })
                
                # Sort by similarity score
                similar_questions.sort(key=lambda x: x['similarity_score'], reverse=True)
                
                return {
                    "similar_questions": similar_questions,
                    "is_duplicate": len(similar_questions) > 0,
                    "duplicate_count": len(similar_questions)
                }
                
        except httpx.HTTPStatusError as e:
            logger.error(f"HTTP error checking question duplicates: {e.response.status_code} - {str(e)}")
            return {
                "error": f"HTTP {e.response.status_code}: Failed to check question duplicates",
                "similar_questions": [],
                "is_duplicate": False
            }
        except httpx.RequestError as e:
            logger.error(f"Network error checking question duplicates: {str(e)}")
            return {
                "error": f"Network error: Failed to connect to service",
                "similar_questions": [],
                "is_duplicate": False
            }
        except Exception as e:
            logger.error(f"Unexpected error checking question duplicates: {str(e)}")
            return {
                "error": f"Error checking question duplicates: {str(e)}",
                "similar_questions": [],
                "is_duplicate": False
            }
    
    async def grade_exam_answer(self, exam_id: int, question_id: int, answer_text: str, max_score: int = 100) -> Dict[str, Any]:
        """Grade an exam answer using NLP"""
        try:
            # Get question details from Question Service via Gateway
            question_response = await self.gateway_client.get(f"/questions/{question_id}")
            
            if question_response.status_code != 200:
                logger.error(f"Failed to fetch question {question_id}: {question_response.status_code}")
                return {
                    "error": "Failed to fetch question details",
                    "exam_id": exam_id,
                    "question_id": question_id,
                    "score": 0,
                    "max_score": max_score,
                    "auto_graded": False
                }
            
            question_data = question_response.json()
            question_content = question_data.get('content', '')
            
            # Grade the answer
            grading_result = await self.grading_service.grade_essay(
                question=question_content,
                answer=answer_text,
                max_score=max_score
            )
            
            # Update exam result in Exam Service via Gateway
            exam_result = {
                "examId": exam_id,
                "questionId": question_id,
                "userAnswer": answer_text,
                "score": grading_result['score'],
                "maxScore": max_score,
                "autoGraded": True,
                "feedback": grading_result['feedback'],
                "gradingDetails": {
                    "strengths": grading_result['strengths'],
                    "weaknesses": grading_result['weaknesses'],
                    "suggestions": grading_result['suggestions'],
                    "confidence": grading_result['confidence']
                }
            }
            
            # Save result to Exam Service via Gateway
            result_response = await self.gateway_client.post(
                f"/exams/{exam_id}/results",
                json=exam_result
            )
            
            if result_response.status_code not in [200, 201]:
                logger.warning(f"Failed to save exam result: {result_response.text}")
                
                return {
                    "exam_id": exam_id,
                    "question_id": question_id,
                    "score": grading_result['score'],
                    "max_score": max_score,
                    "percentage": grading_result['percentage'],
                    "feedback": grading_result['feedback'],
                    "auto_graded": True,
                    "confidence": grading_result['confidence']
                }
                
        except httpx.HTTPStatusError as e:
            logger.error(f"HTTP error grading exam answer: {e.response.status_code} - {str(e)}")
            return {
                "error": f"HTTP {e.response.status_code}: Failed to grade exam answer",
                "exam_id": exam_id,
                "question_id": question_id,
                "score": 0,
                "max_score": max_score,
                "auto_graded": False
            }
        except httpx.RequestError as e:
            logger.error(f"Network error grading exam answer: {str(e)}")
            return {
                "error": f"Network error: Failed to connect to service",
                "exam_id": exam_id,
                "question_id": question_id,
                "score": 0,
                "max_score": max_score,
                "auto_graded": False
            }
        except Exception as e:
            logger.error(f"Unexpected error grading exam answer: {str(e)}")
            return {
                "error": f"Error grading exam answer: {str(e)}",
                "exam_id": exam_id,
                "question_id": question_id,
                "score": 0,
                "max_score": max_score,
                "auto_graded": False
            }
    
    async def batch_grade_exam(self, exam_id: int) -> Dict[str, Any]:
        """Grade all open-ended questions in an exam"""
        try:
            # Get exam details from Exam Service via Gateway
            exam_response = await self.gateway_client.get(f"/exams/{exam_id}")
            
            if exam_response.status_code != 200:
                logger.error(f"Failed to fetch exam {exam_id}: {exam_response.status_code}")
                return {
                    "error": "Failed to fetch exam details",
                    "exam_id": exam_id,
                    "graded_questions": [],
                    "total_questions": 0,
                    "graded_count": 0
                }
            
            exam_data = exam_response.json()
            questions = exam_data.get('questions', [])
            
            graded_questions = []
            total_questions = len(questions)
            
            for question in questions:
                question_id = question.get('id')
                question_type = question.get('type')
                
                # Only grade open-ended questions
                if question_type == 'OPEN_ENDED':
                    # Get user answer for this question via Gateway
                    answer_response = await self.gateway_client.get(
                        f"/exams/{exam_id}/questions/{question_id}/answer"
                    )
                    
                    if answer_response.status_code == 200:
                        answer_data = answer_response.json()
                        answer_text = answer_data.get('answer', '')
                        
                        if answer_text.strip():
                            # Grade the answer
                            grading_result = await self.grade_exam_answer(
                                exam_id, question_id, answer_text, question.get('maxScore', 100)
                            )
                            
                            graded_questions.append({
                                "question_id": question_id,
                                "score": grading_result.get('score', 0),
                                "max_score": grading_result.get('max_score', 100),
                                "auto_graded": True
                            })
                
                return {
                    "exam_id": exam_id,
                    "graded_questions": graded_questions,
                    "total_questions": total_questions,
                    "graded_count": len(graded_questions),
                    "success": True
                }
                
        except httpx.HTTPStatusError as e:
            logger.error(f"HTTP error in batch grading: {e.response.status_code} - {str(e)}")
            return {
                "error": f"HTTP {e.response.status_code}: Failed to batch grade exam",
                "exam_id": exam_id,
                "graded_questions": [],
                "total_questions": 0,
                "graded_count": 0
            }
        except httpx.RequestError as e:
            logger.error(f"Network error in batch grading: {str(e)}")
            return {
                "error": f"Network error: Failed to connect to service",
                "exam_id": exam_id,
                "graded_questions": [],
                "total_questions": 0,
                "graded_count": 0
            }
        except Exception as e:
            logger.error(f"Unexpected error in batch grading: {str(e)}")
            return {
                "error": f"Error in batch grading: {str(e)}",
                "exam_id": exam_id,
                "graded_questions": [],
                "total_questions": 0,
                "graded_count": 0
            }
    
    async def get_question_analytics(self, question_id: int) -> Dict[str, Any]:
        """Get analytics for a specific question"""
        try:
            # Get question details via Gateway
            question_response = await self.gateway_client.get(f"/questions/{question_id}")
            
            if question_response.status_code != 200:
                logger.error(f"Failed to fetch question {question_id}: {question_response.status_code}")
                return {"error": "Failed to fetch question details"}
            
            question_data = question_response.json()
            
            # Get all answers for this question from exams via Gateway
            answers_response = await self.gateway_client.get(f"/questions/{question_id}/answers")
                
                if answers_response.status_code != 200:
                    return {
                        "question_id": question_id,
                        "question_content": question_data.get('content', ''),
                        "analytics": {
                            "total_answers": 0,
                            "average_score": 0,
                            "common_issues": [],
                            "difficulty_level": "Unknown"
                        }
                    }
                
                answers = answers_response.json()
                
                # Analyze answers
                total_answers = len(answers)
                scores = [answer.get('score', 0) for answer in answers if answer.get('score') is not None]
                average_score = sum(scores) / len(scores) if scores else 0
                
                # Analyze common issues
                common_issues = []
                if average_score < 50:
                    common_issues.append("Low average score - question may be too difficult")
                if total_answers < 5:
                    common_issues.append("Insufficient data for analysis")
                
                # Determine difficulty level
                if average_score >= 80:
                    difficulty = "Easy"
                elif average_score >= 60:
                    difficulty = "Medium"
                else:
                    difficulty = "Hard"
                
                return {
                    "question_id": question_id,
                    "question_content": question_data.get('content', ''),
                    "analytics": {
                        "total_answers": total_answers,
                        "average_score": round(average_score, 2),
                        "common_issues": common_issues,
                        "difficulty_level": difficulty,
                        "score_distribution": {
                            "excellent": len([s for s in scores if s >= 80]),
                            "good": len([s for s in scores if 60 <= s < 80]),
                            "fair": len([s for s in scores if 40 <= s < 60]),
                            "poor": len([s for s in scores if s < 40])
                        }
                    }
                }
                
        except httpx.HTTPStatusError as e:
            logger.error(f"HTTP error analyzing question: {e.response.status_code} - {str(e)}")
            return {
                "error": f"HTTP {e.response.status_code}: Failed to analyze question",
                "question_id": question_id
            }
        except httpx.RequestError as e:
            logger.error(f"Network error analyzing question: {str(e)}")
            return {
                "error": f"Network error: Failed to connect to service",
                "question_id": question_id
            }
        except Exception as e:
            logger.error(f"Unexpected error analyzing question: {str(e)}")
            return {
                "error": f"Error analyzing question: {str(e)}",
                "question_id": question_id
            }
