package com.abc.user_service.repository;

import com.abc.user_service.entity.EloHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EloHistoryRepository extends JpaRepository<EloHistory, Long> {
    // Lấy lịch sử Elo của user với phân trang
    Page<EloHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Lấy top N lịch sử gần nhất của user
    List<EloHistory> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Lấy tất cả lịch sử của user
    List<EloHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}