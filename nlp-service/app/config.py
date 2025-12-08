import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    # Service Configuration
    SERVICE_NAME = "nlp-service"
    SERVICE_PORT = int(os.getenv("NLP_SERVICE_PORT", 8088))
    SERVICE_VERSION = "1.0.0"
    
    # External Services - All calls go through API Gateway
    GATEWAY_URL = os.getenv("GATEWAY_URL", "http://gateway-service:8080")
    
    # Legacy service URLs (deprecated - use GATEWAY_URL instead)
    # QUESTION_SERVICE_URL = os.getenv("QUESTION_SERVICE_URL", "http://question-service:8085")
    # EXAM_SERVICE_URL = os.getenv("EXAM_SERVICE_URL", "http://exam-service:8086")
    AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://auth-service:8081")
    
    # JWT Configuration
    JWT_SECRET = os.getenv("JWT_SECRET", "UCIafMmHwgsJKIgg4xVAL/eOvR3ZXD/ZnYE9AfMaMQg=")
    JWT_ALGORITHM = "HS256"
    
    # NLP Model Configuration
    SIMILARITY_THRESHOLD = float(os.getenv("SIMILARITY_THRESHOLD", 0.7))
    GRADING_CONFIDENCE_THRESHOLD = float(os.getenv("GRADING_CONFIDENCE_THRESHOLD", 0.6))
    
    # Model Paths
    SENTENCE_TRANSFORMER_MODEL = "all-MiniLM-L6-v2"
    SPACY_MODEL = "en_core_web_sm"
    
    # Grading Weights
    CONTENT_WEIGHT = 0.4
    STRUCTURE_WEIGHT = 0.2
    LANGUAGE_WEIGHT = 0.2
    RELEVANCE_WEIGHT = 0.2
    
    # Rate Limiting
    RATE_LIMIT_PER_MINUTE = int(os.getenv("RATE_LIMIT_PER_MINUTE", 60))
    
    # Logging
    LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
    
    # Database (if needed for caching)
    REDIS_URL = os.getenv("REDIS_URL", "redis://redis:6379")
    
    # Health Check
    HEALTH_CHECK_INTERVAL = int(os.getenv("HEALTH_CHECK_INTERVAL", 30))

settings = Settings()
