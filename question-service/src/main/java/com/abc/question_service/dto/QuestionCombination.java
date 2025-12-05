package com.abc.question_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a unique combination of metadata for question generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionCombination {
    private Long fieldId;
    private String fieldName;
    private Long topicId;
    private String topicName;
    private Long levelId;
    private String levelName;
    private Long questionTypeId;
    private String questionTypeName;
    private Integer questionCount;
    
    /**
     * Creates a unique key for this combination.
     */
    public String getKey() {
        return fieldId + "-" + topicId + "-" + levelId + "-" + questionTypeId;
    }
}
