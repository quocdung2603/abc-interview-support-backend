package com.abc.social_service.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private boolean valid;
    private String errorMessage;
    @Builder.Default
    private Map<String, String> fieldErrors = new HashMap<>();
    
    public static ValidationResult success() {
        return ValidationResult.builder()
            .valid(true)
            .build();
    }
    
    public static ValidationResult failure(String errorMessage) {
        return ValidationResult.builder()
            .valid(false)
            .errorMessage(errorMessage)
            .build();
    }
    
    public static ValidationResult failure(String errorMessage, Map<String, String> fieldErrors) {
        return ValidationResult.builder()
            .valid(false)
            .errorMessage(errorMessage)
            .fieldErrors(fieldErrors)
            .build();
    }
    
    public void addFieldError(String field, String error) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new HashMap<>();
        }
        this.fieldErrors.put(field, error);
    }
}
