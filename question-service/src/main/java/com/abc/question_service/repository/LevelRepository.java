package com.abc.question_service.repository;

import com.abc.question_service.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    boolean existsByName(String name);
}
