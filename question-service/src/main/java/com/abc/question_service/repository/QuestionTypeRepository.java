package com.abc.question_service.repository;

import com.abc.question_service.entity.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionTypeRepository extends JpaRepository<QuestionType, Long> {
    boolean existsByName(String name);
}
