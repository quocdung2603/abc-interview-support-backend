package com.abc.question_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id")
    private Field field;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_type_id")
    private QuestionType questionType;
    @Column(columnDefinition = "text")
    private String questionContent;
    @Column(columnDefinition = "text")
    private String questionAnswer;
    private Double similarityScore;
    private String status; // PENDING/APPROVED/REJECTED
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private Integer usefulVote;
    private Integer unusefulVote;
    
    // Cascade delete: when question is deleted, all associated answers are also deleted
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();
}


