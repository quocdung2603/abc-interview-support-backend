package com.abc.social_service.validation;

public interface ClassificationValidator {
    ValidationResult validateClassification(Long fieldId, Long topicId, Long levelId);
}
