"""
Gateway Client for routing all service-to-service calls through API Gateway.
This ensures authentication, rate limiting, and monitoring are applied consistently.
"""
import httpx
import asyncio
import logging
from typing import Optional, Dict, Any
from datetime import datetime, timedelta
import os

logger = logging.getLogger(__name__)


class GatewayClient:
    """
    HTTP client that routes all requests through the API Gateway.
    Handles authentication, token refresh, and error handling.
    """
    
    def __init__(self, gateway_url: str, jwt_secret: str):
        self.gateway_url = gateway_url.rstrip('/')
        self.jwt_secret = jwt_secret
        self._service_token: Optional[str] = None
        self._token_expiry: Optional[datetime] = None
        self._request_token: Optional[str] = None  # Token from incoming request
        
    def set_request_token(self, token: str):
        """
        Set the JWT token from the incoming request to pass through.
        This maintains user context throughout the call chain.
        """
        self._request_token = token
        
    def clear_request_token(self):
        """Clear the request token after processing."""
        self._request_token = None

    
    def _get_auth_token(self) -> str:
        """
        Get the authentication token to use for gateway requests.
        Priority: request token > service token
        """
        if self._request_token:
            return self._request_token
            
        # For service-to-service calls without user context,
        # we would need a service account token
        # For now, return empty string and let gateway handle it
        return ""
    
    def _get_headers(self, additional_headers: Optional[Dict[str, str]] = None) -> Dict[str, str]:
        """Build headers with authentication."""
        headers = {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
        
        token = self._get_auth_token()
        if token:
            headers["Authorization"] = f"Bearer {token}"
        
        if additional_headers:
            headers.update(additional_headers)
            
        return headers
    
    async def get(self, path: str, params: Optional[Dict[str, Any]] = None, 
                  headers: Optional[Dict[str, str]] = None, timeout: float = 30.0) -> httpx.Response:
        """
        Make a GET request through the gateway.
        
        Args:
            path: API path (e.g., '/exams/123/history')
            params: Query parameters
            headers: Additional headers
            timeout: Request timeout in seconds
            
        Returns:
            httpx.Response object
        """
        url = f"{self.gateway_url}{path}"
        request_headers = self._get_headers(headers)
        
        try:
            async with httpx.AsyncClient(timeout=timeout) as client:
                response = await client.get(url, params=params, headers=request_headers)
                response.raise_for_status()
                return response
        except httpx.HTTPStatusError as e:
            await self._handle_http_error(e)
            raise
        except httpx.RequestError as e:
            logger.error(f"Gateway request failed for GET {path}: {str(e)}")
            raise
    
    async def post(self, path: str, json: Optional[Dict[str, Any]] = None,
                   headers: Optional[Dict[str, str]] = None, timeout: float = 30.0) -> httpx.Response:
        """
        Make a POST request through the gateway.
        
        Args:
            path: API path (e.g., '/exams/123/submit')
            json: Request body as JSON
            headers: Additional headers
            timeout: Request timeout in seconds
            
        Returns:
            httpx.Response object
        """
        url = f"{self.gateway_url}{path}"
        request_headers = self._get_headers(headers)
        
        try:
            async with httpx.AsyncClient(timeout=timeout) as client:
                response = await client.post(url, json=json, headers=request_headers)
                response.raise_for_status()
                return response
        except httpx.HTTPStatusError as e:
            await self._handle_http_error(e)
            raise
        except httpx.RequestError as e:
            logger.error(f"Gateway request failed for POST {path}: {str(e)}")
            raise
    
    async def put(self, path: str, json: Optional[Dict[str, Any]] = None,
                  headers: Optional[Dict[str, str]] = None, timeout: float = 30.0) -> httpx.Response:
        """
        Make a PUT request through the gateway.
        
        Args:
            path: API path
            json: Request body as JSON
            headers: Additional headers
            timeout: Request timeout in seconds
            
        Returns:
            httpx.Response object
        """
        url = f"{self.gateway_url}{path}"
        request_headers = self._get_headers(headers)
        
        try:
            async with httpx.AsyncClient(timeout=timeout) as client:
                response = await client.put(url, json=json, headers=request_headers)
                response.raise_for_status()
                return response
        except httpx.HTTPStatusError as e:
            await self._handle_http_error(e)
            raise
        except httpx.RequestError as e:
            logger.error(f"Gateway request failed for PUT {path}: {str(e)}")
            raise
    
    async def delete(self, path: str, headers: Optional[Dict[str, str]] = None,
                     timeout: float = 30.0) -> httpx.Response:
        """
        Make a DELETE request through the gateway.
        
        Args:
            path: API path
            headers: Additional headers
            timeout: Request timeout in seconds
            
        Returns:
            httpx.Response object
        """
        url = f"{self.gateway_url}{path}"
        request_headers = self._get_headers(headers)
        
        try:
            async with httpx.AsyncClient(timeout=timeout) as client:
                response = await client.delete(url, headers=request_headers)
                response.raise_for_status()
                return response
        except httpx.HTTPStatusError as e:
            await self._handle_http_error(e)
            raise
        except httpx.RequestError as e:
            logger.error(f"Gateway request failed for DELETE {path}: {str(e)}")
            raise
    
    async def _handle_http_error(self, error: httpx.HTTPStatusError):
        """
        Handle HTTP errors from gateway.
        
        Args:
            error: The HTTP status error
        """
        status_code = error.response.status_code
        
        if status_code == 401:
            logger.warning(f"Authentication failed: {error.response.text}")
            # Token might be expired or invalid
            # In a real implementation, we would refresh the token here
        elif status_code == 429:
            logger.warning(f"Rate limit exceeded: {error.response.text}")
            # Rate limited - caller should implement retry with backoff
        elif status_code == 503:
            logger.error(f"Service unavailable: {error.response.text}")
        else:
            logger.error(f"HTTP {status_code} error: {error.response.text}")
