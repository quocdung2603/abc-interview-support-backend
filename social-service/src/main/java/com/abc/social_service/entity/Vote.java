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
@Table(name = "comment_votes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"commentId", "userId"}))
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long commentId;     // Foreign key to comments table
    private Long userId;        // User who voted
    
    @Column(name = "vote_type", nullable = false)
    private String voteType;    // NEW: "USEFUL" or "NOT_USEFUL"
    
    @Column(name = "vote_weight", nullable = false)
    private Double voteWeight;  // NEW: Weight based on ELO at vote time
    
    private LocalDateTime votedAt;
    
    @PrePersist
    protected void onCreate() {
        votedAt = LocalDateTime.now();
        if (voteType == null) {
            voteType = "USEFUL";  // Default for backward compatibility
        }
        if (voteWeight == null) {
            voteWeight = 1.0;  // Default weight
        }
    }
}
