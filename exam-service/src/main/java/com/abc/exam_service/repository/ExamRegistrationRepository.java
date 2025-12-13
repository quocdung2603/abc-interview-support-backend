package com.abc.exam_service.repository;

import com.abc.exam_service.entity.ExamRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration, Long> {
    boolean existsByExamIdAndUserId(Long examId, Long userId);
    
    boolean existsByExamIdAndUserIdAndRegistrationStatus(Long examId, Long userId, String registrationStatus);
    
    @Query("SELECT er FROM ExamRegistration er JOIN FETCH er.exam WHERE er.exam.id = :examId")
    Page<ExamRegistration> findByExamId(Long examId, Pageable pageable);
    
    @Query("SELECT er FROM ExamRegistration er JOIN FETCH er.exam WHERE er.userId = :userId")
    Page<ExamRegistration> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT er FROM ExamRegistration er JOIN FETCH er.exam WHERE er.exam.id = :examId AND er.userId = :userId")
    java.util.Optional<ExamRegistration> findByExamIdAndUserId(Long examId, Long userId);
    
    @Query("SELECT er FROM ExamRegistration er JOIN FETCH er.exam WHERE er.exam.id = :examId AND er.userId = :userId AND er.registrationStatus = :status")
    java.util.Optional<ExamRegistration> findByExamIdAndUserIdAndRegistrationStatus(Long examId, Long userId, String status);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ExamRegistration er WHERE er.exam.id = :examId")
    void deleteByExamId(Long examId);
}
