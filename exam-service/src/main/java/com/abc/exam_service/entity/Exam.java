package com.abc.exam_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "exams")
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String examType; // VIRTUAL, RECRUITER
    private String title;
    private String position;
    
    // Numeric IDs for field, topics, level, and question types
    private Long fieldId;
    private String topicIds;        // JSON array of topic IDs
    private Long levelId;
    private String questionTypeIds; // JSON array of question type IDs
    
    private Integer questionCount;
    private Integer duration; // minutes
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED
    private String language;
    private LocalDateTime createdAt;
    private Long createdBy;
}
