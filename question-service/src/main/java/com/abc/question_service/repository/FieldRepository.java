package com.abc.question_service.repository;

import com.abc.question_service.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    boolean existsByName(String name);
}
