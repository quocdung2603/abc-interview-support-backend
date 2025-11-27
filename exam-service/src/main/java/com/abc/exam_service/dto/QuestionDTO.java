package com.abc.exam_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDTO {
    private Long id;
    
    // Numeric IDs only (encoding independent)
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    private Long questionTypeId;
    
    private String questionText;
    private String questionAnswer;
}
