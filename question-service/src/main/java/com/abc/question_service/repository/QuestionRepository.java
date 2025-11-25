package com.abc.question_service.repository;

import com.abc.question_service.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    @EntityGraph(attributePaths = {"field", "topic", "level", "questionType"})
    Page<Question> findAll(Pageable pageable);
    
    @EntityGraph(attributePaths = {"field", "topic", "level", "questionType"})
    Page<Question> findByTopicId(Long topicId, Pageable pageable);
    
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.field " +
           "LEFT JOIN FETCH q.topic " +
           "LEFT JOIN FETCH q.level " +
           "LEFT JOIN FETCH q.questionType " +
           "WHERE q.id = :id")
    Question findByIdWithRelationships(@Param("id") Long id);
    
    // Search questions by numeric IDs
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.field f " +
           "LEFT JOIN FETCH q.topic t " +
           "LEFT JOIN FETCH q.level l " +
           "LEFT JOIN FETCH q.questionType qt " +
           "WHERE (:fieldId IS NULL OR f.id = :fieldId) " +
           "AND (:topicIds IS NULL OR t.id IN :topicIds) " +
           "AND (:levelId IS NULL OR l.id = :levelId) " +
           "AND (:questionTypeId IS NULL OR qt.id = :questionTypeId) " +
           "AND q.status = 'APPROVED'")
    List<Question> searchByIds(@Param("fieldId") Long fieldId,
                                @Param("topicIds") List<Long> topicIds,
                                @Param("levelId") Long levelId,
                                @Param("questionTypeId") Long questionTypeId);
}
