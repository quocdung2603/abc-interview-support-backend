package com.abc.social_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long postId;        // Foreign key to posts table
    private Long userId;        // User who created the comment
    
    @Column(columnDefinition = "text")
    private String content;
    
    private Integer voteCount;  // Deprecated - kept for backward compatibility
    
    @Column(name = "weighted_vote_score")
    private Double weightedVoteScore;  // NEW: Sum of weighted votes
    
    @Column(name = "edit_count")
    private Integer editCount;  // NEW: Track number of edits
    
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // NEW: Track last edit time
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (voteCount == null) {
            voteCount = 0;
        }
        if (weightedVoteScore == null) {
            weightedVoteScore = 0.0;
        }
        if (editCount == null) {
            editCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate vote percentage capped at 0-100%
     * @return Vote percentage
     */
    public Double getVotePercentage() {
        if (weightedVoteScore == null) {
            return 0.0;
        }
        return Math.min(100.0, Math.max(0.0, weightedVoteScore));
    }
}
