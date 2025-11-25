package com.abc.question_service.repository;

import com.abc.question_service.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);
    
    // Delete all answers associated with a question
    void deleteByQuestionId(Long questionId);
    
    // Count answers for a question
    long countByQuestionId(Long questionId);
}
