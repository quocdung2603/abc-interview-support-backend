package com.abc.exam_service.repository;

import com.abc.exam_service.entity.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    @Query("SELECT r FROM Result r WHERE r.exam.id = :examId")
    Page<Result> findByExamId(@Param("examId") Long examId, Pageable pageable);
    
    @Query("SELECT r FROM Result r WHERE r.exam.id = :examId")
    java.util.List<Result> findByExamId(@Param("examId") Long examId);
    
    Page<Result> findByUserId(Long userId, Pageable pageable);
    
    java.util.List<Result> findByUserId(Long userId);
    
    java.util.Optional<Result> findTopByExamIdAndUserIdOrderByCompletedAtDesc(Long examId, Long userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Result r WHERE r.exam.id = :examId")
    void deleteByExamId(Long examId);
}
