"""
Property-based tests for Postman collection URL patterns.

Feature: gateway-routing-standardization, Property 6: Postman collection URL pattern consistency
Validates: Requirements 3.1, 3.2, 3.3

Property: For any API request in the Postman collection, the request URL should use the {{gateway_url}}
variable and follow the pattern {{gateway_url}}/service-path.
"""
import json
import re
import pytest
from pathlib import Path


class TestPostmanCollections:
    """Test that Postman collections use gateway URL patterns."""
    
    @pytest.fixture
    def collection_files(self):
        """Get all Postman collection files."""
        return [
            "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json",
            "postman-collections/ABC-Interview-Verified-Complete.postman_collection.json"
        ]
    
    def load_collection(self, file_path):
        """Load a Postman collection file."""
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    def extract_all_requests(self, item):
        """Recursively extract all requests from collection items."""
        requests = []
        
        if isinstance(item, dict):
            if 'request' in item:
                requests.append(item['request'])
            if 'item' in item:
                for sub_item in item['item']:
                    requests.extend(self.extract_all_requests(sub_item))
        elif isinstance(item, list):
            for sub_item in item:
                requests.extend(self.extract_all_requests(sub_item))
        
        return requests
    
    def test_exam_endpoints_use_gateway_url(self, collection_files):
        """
        Property: All exam-service endpoints should use {{gateway_url}}/exams pattern.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            requests = self.extract_all_requests(collection.get('item', []))
            
            for request in requests:
                url = request.get('url', {})
                
                # Get raw URL string
                raw_url = url.get('raw', '') if isinstance(url, dict) else str(url)
                
                # Check if this is an exam endpoint
                if '/exams' in raw_url:
                    # Should use gateway_url variable
                    assert '{{gateway_url}}/exams' in raw_url or '{{base_url}}/exams' in raw_url or '{{baseUrl}}/exams' in raw_url, \
                        f"Exam endpoint should use gateway URL pattern: {raw_url}"
                    
                    # Should NOT use exam_service_url
                    assert '{{exam_service_url}}' not in raw_url, \
                        f"Should not use exam_service_url: {raw_url}"
    
    def test_question_endpoints_use_gateway_url(self, collection_files):
        """
        Property: All question-service endpoints should use {{gateway_url}}/questions pattern.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            requests = self.extract_all_requests(collection.get('item', []))
            
            for request in requests:
                url = request.get('url', {})
                raw_url = url.get('raw', '') if isinstance(url, dict) else str(url)
                
                if '/questions' in raw_url:
                    assert '{{gateway_url}}/questions' in raw_url or '{{base_url}}/questions' in raw_url or '{{baseUrl}}/questions' in raw_url, \
                        f"Question endpoint should use gateway URL pattern: {raw_url}"
                    
                    assert '{{question_service_url}}' not in raw_url, \
                        f"Should not use question_service_url: {raw_url}"
    
    def test_user_endpoints_use_gateway_url(self, collection_files):
        """
        Property: All user-service endpoints should use {{gateway_url}}/users pattern.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            requests = self.extract_all_requests(collection.get('item', []))
            
            for request in requests:
                url = request.get('url', {})
                raw_url = url.get('raw', '') if isinstance(url, dict) else str(url)
                
                if '/users' in raw_url:
                    assert '{{gateway_url}}/users' in raw_url or '{{base_url}}/users' in raw_url or '{{baseUrl}}/users' in raw_url, \
                        f"User endpoint should use gateway URL pattern: {raw_url}"
                    
                    assert '{{user_service_url}}' not in raw_url, \
                        f"Should not use user_service_url: {raw_url}"
    
    def test_no_direct_service_urls_in_requests(self, collection_files):
        """
        Property: No request should use direct service URL variables.
        """
        forbidden_patterns = [
            '{{exam_service_url}}',
            '{{question_service_url}}',
            '{{user_service_url}}',
            '{{auth_service_url}}',
            '{{career_service_url}}',
            '{{news_service_url}}',
            '{{social_service_url}}'
        ]
        
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            requests = self.extract_all_requests(collection.get('item', []))
            
            for request in requests:
                url = request.get('url', {})
                raw_url = url.get('raw', '') if isinstance(url, dict) else str(url)
                
                for pattern in forbidden_patterns:
                    assert pattern not in raw_url, \
                        f"Request should not use {pattern}: {raw_url}"
    
    def test_all_service_paths_use_gateway(self, collection_files):
        """
        Property: All service paths should go through gateway or base URL.
        """
        service_paths = ['/exams', '/questions', '/users', '/auth', '/career', '/news', '/posts', '/comments']
        
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            requests = self.extract_all_requests(collection.get('item', []))
            
            for request in requests:
                url = request.get('url', {})
                raw_url = url.get('raw', '') if isinstance(url, dict) else str(url)
                
                # Check if URL contains any service path
                for path in service_paths:
                    if path in raw_url:
                        # Should use gateway_url, base_url, or baseUrl
                        assert ('{{gateway_url}}' in raw_url or 
                                '{{base_url}}' in raw_url or 
                                '{{baseUrl}}' in raw_url), \
                            f"Service path {path} should use gateway/base URL: {raw_url}"
                        break



"""
Property-based tests for Postman collection variable cleanup.

Feature: gateway-routing-standardization, Property 7: Postman collection variable cleanup
Validates: Requirements 3.5

Property: For any variable defined in the Postman collection, the variable should not be a direct
service URL (e.g., exam_service_url, question_service_url should not exist).
"""


class TestPostmanVariableCleanup:
    """Test that Postman collections don't contain direct service URL variables."""
    
    @pytest.fixture
    def collection_files(self):
        """Get all Postman collection files."""
        return [
            "ABC-Interview-VERIFIED-Complete.postman_collection.backup.json",
            "postman-collections/ABC-Interview-Verified-Complete.postman_collection.json"
        ]
    
    def load_collection(self, file_path):
        """Load a Postman collection file."""
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    
    def test_no_exam_service_url_variable(self, collection_files):
        """
        Property: Collection should not define exam_service_url variable.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            variable_keys = [v.get('key') for v in variables]
            assert 'exam_service_url' not in variable_keys, \
                f"Collection should not have exam_service_url variable in {file_path}"
    
    def test_no_question_service_url_variable(self, collection_files):
        """
        Property: Collection should not define question_service_url variable.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            variable_keys = [v.get('key') for v in variables]
            assert 'question_service_url' not in variable_keys, \
                f"Collection should not have question_service_url variable in {file_path}"
    
    def test_no_user_service_url_variable(self, collection_files):
        """
        Property: Collection should not define user_service_url variable.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            variable_keys = [v.get('key') for v in variables]
            assert 'user_service_url' not in variable_keys, \
                f"Collection should not have user_service_url variable in {file_path}"
    
    def test_no_direct_service_url_variables(self, collection_files):
        """
        Property: Collection should not define any direct service URL variables.
        """
        forbidden_variables = [
            'exam_service_url',
            'question_service_url',
            'user_service_url',
            'auth_service_url',
            'career_service_url',
            'news_service_url',
            'social_service_url'
        ]
        
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            variable_keys = [v.get('key') for v in variables]
            
            for forbidden_var in forbidden_variables:
                assert forbidden_var not in variable_keys, \
                    f"Collection should not have {forbidden_var} variable in {file_path}"
    
    def test_gateway_url_variable_exists(self, collection_files):
        """
        Property: Collection should define gateway_url variable.
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            variable_keys = [v.get('key') for v in variables]
            
            # Should have gateway_url OR base_url/baseUrl (which serves same purpose)
            has_gateway_var = ('gateway_url' in variable_keys or 
                              'base_url' in variable_keys or 
                              'baseUrl' in variable_keys)
            
            assert has_gateway_var, \
                f"Collection should have gateway_url (or base_url/baseUrl) variable in {file_path}"
    
    def test_gateway_url_points_to_gateway(self, collection_files):
        """
        Property: gateway_url variable should point to gateway service (port 8080).
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            # Find gateway_url or base_url variable
            gateway_var = None
            for v in variables:
                if v.get('key') in ['gateway_url', 'base_url', 'baseUrl']:
                    gateway_var = v
                    break
            
            if gateway_var:
                value = gateway_var.get('value', '')
                # Should point to port 8080 (gateway)
                assert ':8080' in value or value.endswith('/8080'), \
                    f"Gateway URL should point to port 8080: {value} in {file_path}"
    
    def test_all_variables_are_valid(self, collection_files):
        """
        Property: All variables should have valid structure (key, value, type).
        """
        for file_path in collection_files:
            if not Path(file_path).exists():
                pytest.skip(f"File not found: {file_path}")
            
            collection = self.load_collection(file_path)
            variables = collection.get('variable', [])
            
            for var in variables:
                assert 'key' in var, f"Variable should have 'key' field in {file_path}"
                assert 'value' in var, f"Variable should have 'value' field in {file_path}"
                assert isinstance(var.get('key'), str), f"Variable key should be string in {file_path}"
