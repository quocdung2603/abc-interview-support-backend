package com.abc.question_service.repository;

import com.abc.question_service.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    
    @Query("SELECT t FROM Topic t LEFT JOIN FETCH t.field WHERE t.id = :id")
    Topic findByIdWithField(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"field"})
    Page<Topic> findAll(Pageable pageable);
    
    boolean existsByNameAndFieldId(String name, Long fieldId);
    
    @Query("SELECT COUNT(t) FROM Topic t WHERE t.field.id = :fieldId")
    long countByFieldId(@Param("fieldId") Long fieldId);
}
