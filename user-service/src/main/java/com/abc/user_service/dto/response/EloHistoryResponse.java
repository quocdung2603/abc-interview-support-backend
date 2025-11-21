package com.abc.user_service.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EloHistoryResponse {
    private Long id;
    private Long userId;
    private String action;
    private Integer points;
    private String description;
    private LocalDateTime createdAt;
}
