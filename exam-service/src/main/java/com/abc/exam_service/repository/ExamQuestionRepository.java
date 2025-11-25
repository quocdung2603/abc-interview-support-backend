package com.abc.exam_service.repository;

import com.abc.exam_service.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ExamQuestion eq WHERE eq.exam.id = :examId")
    void deleteByExamId(Long examId);
    
    long countByExamId(Long examId);
    
    @Query("SELECT MAX(eq.orderNumber) FROM ExamQuestion eq WHERE eq.exam.id = :examId")
    Integer findMaxOrderNumberByExamId(Long examId);
    
    List<ExamQuestion> findByExamIdOrderByOrderNumberAsc(Long examId);
}
