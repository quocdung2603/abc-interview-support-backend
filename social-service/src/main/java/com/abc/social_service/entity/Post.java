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
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long userId;        // User who created the post
    private Long fieldId;       // Reference to field/category
    private Long topicId;       // Reference to topic
    private Long levelId;       // Optional difficulty level
    
    @Column(length = 20, nullable = false)
    private String postType = "DISCUSSION";  // DISCUSSION or QUESTION
    
    @Column(length = 20, nullable = false)
    private String status = "DRAFT";  // DRAFT, PUBLISHED, or LOCKED
    
    private String title;
    
    @Column(columnDefinition = "text")
    private String content;
    
    private LocalDateTime lockTime;  // Time when post becomes locked
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        
        // Set default values if not provided
        if (postType == null || postType.isEmpty()) {
            postType = "DISCUSSION";
        }
        if (status == null || status.isEmpty()) {
            status = "DRAFT";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
