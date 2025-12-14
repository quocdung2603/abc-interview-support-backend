package com.abc.social_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private Integer voteCount;
    private Double weightedVoteScore;
    private Double votePercentage;  // Deprecated
    private Integer usefulVoteCount;
    private Integer notUsefulVoteCount;
    private Double usefulPercentage;
    private Double notUsefulPercentage;
    private Integer editCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
