package com.abc.exam_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExamResponse {
    private Long id;
    private Long userId;
    private String examType;
    private String title;
    private String position;
    
    // New: Numeric IDs for field, topic, and level
    private Long fieldId;
    private Long topicId;
    private Long levelId;
    
    // Deprecated: Keep for backward compatibility
    private List<Long> topics;
    private List<Long> questionTypes;
    
    private Integer questionCount;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String language;
    private LocalDateTime createdAt;
    private Long createdBy;
    private List<QuestionDTO> questions; // Danh sách câu hỏi kèm theo
}
