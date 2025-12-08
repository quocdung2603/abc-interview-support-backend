"""
Property-based tests for NLP service gateway routing.

Feature: gateway-routing-standardization, Property 4: NLP service gateway routing
Validates: Requirements 2.1, 2.2, 2.3

Property: For any API call from NLP service to other services (exam-service, question-service),
the call should go through the gateway URL and include valid JWT authentication headers.
"""
import pytest
from hypothesis import given, strategies as st, settings
from unittest.mock import AsyncMock, patch, MagicMock
import httpx
from app.services.integration_service import IntegrationService
from app.config import settings


# Strategy for generating valid exam IDs
exam_ids = st.integers(min_value=1, max_value=10000)

# Strategy for generating valid question IDs
question_ids = st.integers(min_value=1, max_value=10000)

# Strategy for generating question text
question_texts = st.text(min_size=10, max_size=500)

# Strategy for generating answer text
answer_texts = st.text(min_size=10, max_size=1000)


class TestGatewayRouting:
    """Test that all NLP service calls go through the gateway."""
    
    @pytest.mark.asyncio
    @given(question_text=question_texts)
    @settings(max_examples=100)
    async def test_check_duplicates_uses_gateway(self, question_text):
        """
        Property: check_question_duplicates should call gateway URL, not direct service URL.
        """
        service = IntegrationService()
        
        # Mock the gateway client
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            # Setup mock response
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = []
            mock_get.return_value = mock_response
            
            # Call the method
            await service.check_question_duplicates(question_text)
            
            # Verify gateway client was called
            mock_get.assert_called_once()
            
            # Verify the path starts with /questions (gateway path, not full URL)
            call_args = mock_get.call_args
            path = call_args[0][0] if call_args[0] else call_args[1].get('path', '')
            assert path.startswith('/questions'), \
                f"Expected gateway path starting with /questions, got {path}"
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids, question_id=question_ids, answer_text=answer_texts)
    @settings(max_examples=100)
    async def test_grade_answer_uses_gateway(self, exam_id, question_id, answer_text):
        """
        Property: grade_exam_answer should call gateway URL for both question and exam services.
        """
        service = IntegrationService()
        
        # Mock the gateway client
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get, \
             patch.object(service.gateway_client, 'post', new_callable=AsyncMock) as mock_post:
            
            # Setup mock responses
            question_response = MagicMock()
            question_response.status_code = 200
            question_response.json.return_value = {'content': 'Test question'}
            mock_get.return_value = question_response
            
            result_response = MagicMock()
            result_response.status_code = 200
            result_response.json.return_value = {}
            mock_post.return_value = result_response
            
            # Mock grading service
            with patch.object(service.grading_service, 'grade_essay', new_callable=AsyncMock) as mock_grade:
                mock_grade.return_value = {
                    'score': 80,
                    'percentage': 80.0,
                    'feedback': 'Good',
                    'strengths': [],
                    'weaknesses': [],
                    'suggestions': [],
                    'confidence': 0.9
                }
                
                # Call the method
                await service.grade_exam_answer(exam_id, question_id, answer_text)
                
                # Verify gateway client was called for GET (question)
                assert mock_get.called, "Gateway GET should be called for question"
                get_path = mock_get.call_args[0][0]
                assert get_path.startswith('/questions/'), \
                    f"Expected gateway path /questions/*, got {get_path}"
                
                # Verify gateway client was called for POST (exam result)
                assert mock_post.called, "Gateway POST should be called for exam result"
                post_path = mock_post.call_args[0][0]
                assert post_path.startswith('/exams/'), \
                    f"Expected gateway path /exams/*, got {post_path}"
    
    @pytest.mark.asyncio
    @given(exam_id=exam_ids)
    @settings(max_examples=100)
    async def test_batch_grade_uses_gateway(self, exam_id):
        """
        Property: batch_grade_exam should call gateway URL for exam service.
        """
        service = IntegrationService()
        
        # Mock the gateway client
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            # Setup mock response
            exam_response = MagicMock()
            exam_response.status_code = 200
            exam_response.json.return_value = {
                'id': exam_id,
                'questions': []  # Empty to avoid further calls
            }
            mock_get.return_value = exam_response
            
            # Call the method
            await service.batch_grade_exam(exam_id)
            
            # Verify gateway client was called
            mock_get.assert_called_once()
            
            # Verify the path starts with /exams (gateway path)
            call_args = mock_get.call_args
            path = call_args[0][0]
            assert path.startswith('/exams/'), \
                f"Expected gateway path /exams/*, got {path}"
    
    @pytest.mark.asyncio
    @given(question_id=question_ids)
    @settings(max_examples=100)
    async def test_analytics_uses_gateway(self, question_id):
        """
        Property: get_question_analytics should call gateway URL for both services.
        """
        service = IntegrationService()
        
        # Mock the gateway client
        with patch.object(service.gateway_client, 'get', new_callable=AsyncMock) as mock_get:
            # Setup mock responses
            def side_effect(path, *args, **kwargs):
                response = MagicMock()
                response.status_code = 200
                if '/questions/' in path and '/answers' not in path:
                    response.json.return_value = {'id': question_id, 'content': 'Test'}
                else:
                    response.json.return_value = []
                return response
            
            mock_get.side_effect = side_effect
            
            # Call the method
            await service.get_question_analytics(question_id)
            
            # Verify gateway client was called at least twice
            assert mock_get.call_count >= 2, "Should call gateway for question and answers"
            
            # Verify all calls use gateway paths
            for call in mock_get.call_args_list:
                path = call[0][0]
                assert path.startswith('/questions/') or path.startswith('/exams/'), \
                    f"Expected gateway path, got {path}"
    
    def test_gateway_client_initialization(self):
        """
        Property: IntegrationService should initialize with gateway client using GATEWAY_URL.
        """
        service = IntegrationService()
        
        # Verify gateway client exists
        assert hasattr(service, 'gateway_client'), "Service should have gateway_client"
        assert service.gateway_client is not None, "Gateway client should be initialized"
        
        # Verify gateway URL is configured
        assert service.gateway_client.gateway_url == settings.GATEWAY_URL.rstrip('/'), \
            f"Gateway client should use GATEWAY_URL from settings"
    
    @pytest.mark.asyncio
    async def test_gateway_client_includes_auth_header(self):
        """
        Property: Gateway client should include Authorization header when token is set.
        """
        service = IntegrationService()
        
        # Set a test token
        test_token = "test_jwt_token_12345"
        service.gateway_client.set_request_token(test_token)
        
        # Mock httpx to capture the actual request
        with patch('httpx.AsyncClient') as mock_client_class:
            mock_client = AsyncMock()
            mock_client_class.return_value.__aenter__.return_value = mock_client
            
            mock_response = MagicMock()
            mock_response.status_code = 200
            mock_response.json.return_value = []
            mock_client.get.return_value = mock_response
            
            # Make a request
            await service.gateway_client.get('/questions')
            
            # Verify Authorization header was included
            call_kwargs = mock_client.get.call_args[1]
            headers = call_kwargs.get('headers', {})
            assert 'Authorization' in headers, "Authorization header should be present"
            assert headers['Authorization'] == f'Bearer {test_token}', \
                "Authorization header should contain the token"
