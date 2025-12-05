package com.abc.exam_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuestionMetadata {
    private Long fieldId;
    private String fieldName;
    private List<Long> topicIds;
    private List<String> topicNames;
    private Long levelId;
    private String levelName;
    private Long questionTypeId;
    private String questionTypeName;
}
