package com.abc.exam_service.repository;

import com.abc.exam_service.entity.UserAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    Page<UserAnswer> findByExamIdAndUserId(Long examId, Long userId, Pageable pageable);
    
    java.util.List<UserAnswer> findByExamIdAndUserIdOrderByCreatedAtAsc(Long examId, Long userId);
    
    java.util.Optional<UserAnswer> findByExamIdAndUserIdAndQuestionId(Long examId, Long userId, Long questionId);
    
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.exam.id = :examId AND ua.userId = :userId AND ua.createdAt > :startTime AND ua.createdAt <= :endTime ORDER BY ua.createdAt ASC")
    java.util.List<UserAnswer> findByExamIdAndUserIdAndCreatedAtBetween(
        @org.springframework.data.repository.query.Param("examId") Long examId, 
        @org.springframework.data.repository.query.Param("userId") Long userId, 
        @org.springframework.data.repository.query.Param("startTime") java.time.LocalDateTime startTime, 
        @org.springframework.data.repository.query.Param("endTime") java.time.LocalDateTime endTime);
    
    @Query("SELECT ua FROM UserAnswer ua WHERE ua.exam.id = :examId AND ua.userId = :userId AND ua.createdAt <= :endTime ORDER BY ua.createdAt ASC")
    java.util.List<UserAnswer> findByExamIdAndUserIdAndCreatedAtBefore(
        @org.springframework.data.repository.query.Param("examId") Long examId, 
        @org.springframework.data.repository.query.Param("userId") Long userId, 
        @org.springframework.data.repository.query.Param("endTime") java.time.LocalDateTime endTime);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserAnswer ua WHERE ua.exam.id = :examId")
    void deleteByExamId(Long examId);
}
