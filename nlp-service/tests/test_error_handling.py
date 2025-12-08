"""
Property-based tests for NLP service error handling.

Feature: gateway-routing-standardization, Property 5: NLP service error handling
Validates: Requirements 2.5

Property: For any failed service-to-service call from NLP service, the service should handle
the error gracefully without crashing and log appropriate error messages.
"""
import pytest
from hypothesis import given, strategies as st, settings
from unittest.mock import AsyncMock, patch, MagicMock
import httpx
from app.services.integration_service import IntegrationService


# Strategies for test data
exam_ids = st.integers(min_value=1, max_value=10000)
question_ids = st.integers(min_value=1, max_value=10000)
question_texts = st.text(min_size=10, max_size=500)
answer_texts = st.text(min_size=10, max_size=1000)

# HTTP error status codes
http_error_codes = st.sampled_from([400, 401, 403, 404, 429, 500, 502, 503, 504])


class TestErrorHandling:
    """Test that NLP service handles errors gracefully."""
    
    @pytest.mark.asyncio
    @given(question_text=question_texts, status_code=http_error_codes)
    @settings(max_examples=100)
    async def test_check_duplicates_handles_http_errors(self, question_text, status_code):
        """
        Property: check_question_duplicates should handle HTTP errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise HTTP error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = status_code
            mock_response.text = f"Error {status_code}"
            
            error = httpx.HTTPStatusError(
                message=f"HTTP {status_code}",
                request=MagicMock(),
                response=mock_response
            )
            mock_get.side_effect = error
            
            # Call should not crash
            result = await service.check_question_duplicates(question_text)
            
            # Should return error response, not raise exception
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert result['is_duplicate'] == False, "Should indicate no duplicate on error"
            assert result['similar_questions'] == [], "Should return empty list on error"
    
    @pytest.mark.asyncio
    @given(question_text=question_texts)
    @settings(max_examples=100)
    async def test_check_duplicates_handles_network_errors(self, question_text):
        """
        Property: check_question_duplicates should handle network errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise network error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_get.side_effect = httpx.RequestError("Connection failed")
            
            # Call should not crash
            result = await service.check_question_duplicates(question_text)
            
            # Should return error response
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert 'Network error' in result['error'] or 'error' in result['error'].lower()
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids, question_id=question_ids, answer_text=answer_texts, status_code=http_error_codes)
    @settings(max_examples=100)
    async def test_grade_answer_handles_http_errors(self, exam_id, question_id, answer_text, status_code):
        """
        Property: grade_exam_answer should handle HTTP errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise HTTP error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = status_code
            mock_response.text = f"Error {status_code}"
            
            error = httpx.HTTPStatusError(
                message=f"HTTP {status_code}",
                request=MagicMock(),
                response=mock_response
            )
            mock_get.side_effect = error
            
            # Call should not crash
            result = await service.grade_exam_answer(exam_id, question_id, answer_text)
            
            # Should return error response with proper structure
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert result['exam_id'] == exam_id, "Should preserve exam_id"
            assert result['question_id'] == question_id, "Should preserve question_id"
            assert result['score'] == 0, "Should return 0 score on error"
            assert result['auto_graded'] == False, "Should indicate not graded on error"
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids, question_id=question_ids, answer_text=answer_texts)
    @settings(max_examples=100)
    async def test_grade_answer_handles_network_errors(self, exam_id, question_id, answer_text):
        """
        Property: grade_exam_answer should handle network errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise network error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_get.side_effect = httpx.RequestError("Connection timeout")
            
            # Call should not crash
            result = await service.grade_exam_answer(exam_id, question_id, answer_text)
            
            # Should return error response
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert result['auto_graded'] == False, "Should indicate not graded"
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids, status_code=http_error_codes)
    @settings(max_examples=100)
    async def test_batch_grade_handles_http_errors(self, exam_id, status_code):
        """
        Property: batch_grade_exam should handle HTTP errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise HTTP error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = status_code
            mock_response.text = f"Error {status_code}"
            
            error = httpx.HTTPStatusError(
                message=f"HTTP {status_code}",
                request=MagicMock(),
                response=mock_response
            )
            mock_get.side_effect = error
            
            # Call should not crash
            result = await service.batch_grade_exam(exam_id)
            
            # Should return error response with proper structure
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert result['exam_id'] == exam_id, "Should preserve exam_id"
            assert result['graded_count'] == 0, "Should return 0 graded count on error"
            assert result['graded_questions'] == [], "Should return empty list on error"
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids)
    @settings(max_examples=100)
    async def test_batch_grade_handles_network_errors(self, exam_id):
        """
        Property: batch_grade_exam should handle network errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise network error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_get.side_effect = httpx.RequestError("DNS resolution failed")
            
            # Call should not crash
            result = await service.batch_grade_exam(exam_id)
            
            # Should return error response
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert result['graded_count'] == 0, "Should return 0 graded count"
    
    @pytest.mark.asyncio
    @given(question_id=question_ids, status_code=http_error_codes)
    @settings(max_examples=100)
    async def test_analytics_handles_http_errors(self, question_id, status_code):
        """
        Property: get_question_analytics should handle HTTP errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise HTTP error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_response = MagicMock()
            mock_response.status_code = status_code
            mock_response.text = f"Error {status_code}"
            
            error = httpx.HTTPStatusError(
                message=f"HTTP {status_code}",
                request=MagicMock(),
                response=mock_response
            )
            mock_get.side_effect = error
            
            # Call should not crash
            result = await service.get_question_analytics(question_id)
            
            # Should return error response
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
            assert result['question_id'] == question_id, "Should preserve question_id"
    
    @pytest.mark.asyncio
    @given(question_id=question_ids)
    @settings(max_examples=100)
    async def test_analytics_handles_network_errors(self, question_id):
        """
        Property: get_question_analytics should handle network errors gracefully.
        """
        service = IntegrationService()
        
        # Mock the gateway client to raise network error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_get.side_effect = httpx.RequestError("Network unreachable")
            
            # Call should not crash
            result = await service.get_question_analytics(question_id)
            
            # Should return error response
            assert isinstance(result, dict), "Should return dict response"
            assert 'error' in result, "Should contain error field"
    
    @pytest.mark.asyncio
    @given(question_text=question_texts)
    @settings(max_examples=50)
    async def test_error_responses_are_json_serializable(self, question_text):
        """
        Property: All error responses should be JSON serializable.
        """
        import json
        service = IntegrationService()
        
        # Mock to cause error
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            mock_get.side_effect = httpx.RequestError("Test error")
            
            result = await service.check_question_duplicates(question_text)
            
            # Should be JSON serializable
            try:
                json.dumps(result)
                serializable = True
            except (TypeError, ValueError):
                serializable = False
            
            assert serializable, "Error response should be JSON serializable"
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids, question_id=question_ids, answer_text=answer_texts)
    @settings(max_examples=50)
    async def test_partial_failure_in_grading(self, exam_id, question_id, answer_text):
        """
        Property: If question fetch succeeds but result save fails, should still return grading result.
        """
        service = IntegrationService()
        
        # Mock question fetch to succeed, result save to fail
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get, \
             patch.object(service.gateway_client, 'post', new_callable=AsyncMock) as mock_post:
            
            # Question fetch succeeds
            question_response = MagicMock()
            question_response.status_code = 200
            question_response.json.return_value = {'content': 'Test question'}
            mock_get.return_value = question_response
            
            # Result save fails
            result_response = MagicMock()
            result_response.status_code = 500
            result_response.text = "Internal server error"
            mock_post.return_value = result_response
            
            # Mock grading service
            with patch.object(service.grading_service, 'grade_essay', new_callable=AsyncMock) as mock_grade:
                mock_grade.return_value = {
                    'score': 75,
                    'percentage': 75.0,
                    'feedback': 'Good answer',
                    'strengths': ['Clear'],
                    'weaknesses': [],
                    'suggestions': [],
                    'confidence': 0.85
                }
                
                # Should still return grading result even if save fails
                result = await service.grade_exam_answer(exam_id, question_id, answer_text)
                
                # Should have grading results
                assert 'score' in result, "Should contain score"
                assert 'feedback' in result, "Should contain feedback"
                assert result['auto_graded'] == True, "Should indicate graded"
