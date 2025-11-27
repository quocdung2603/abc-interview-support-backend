package com.abc.social_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteResponse {
    private Long commentId;
    private Integer voteCount;
    private Double voteWeight;
    private Double weightedVoteScore;
    private Double votePercentage;
    private String message;
}
