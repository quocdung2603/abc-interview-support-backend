package com.abc.exam_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    
    // Numeric IDs (preferred - encoding independent)
    private Long fieldId;
    private List<Long> topicIds;
    private Long levelId;
    private Long questionTypeId;
    
    // Text names (for display - may have encoding issues)
    private String field;
    private List<String> topics;
    private String level;
    private String questionType;
    
    private String questionText;
    private String questionAnswer; // Đáp án của câu hỏi
}
