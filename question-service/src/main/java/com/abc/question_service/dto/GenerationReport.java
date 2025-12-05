package com.abc.question_service.dto;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class GenerationReport {
    private Integer requestedCount;
    private Integer generatedCount;
    private Integer failedCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private Map<String, Integer> distributionByField = new HashMap<>();
    private List<String> errors = new ArrayList<>();
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    public void incrementFieldDistribution(String fieldName) {
        distributionByField.merge(fieldName, 1, Integer::sum);
    }
}
