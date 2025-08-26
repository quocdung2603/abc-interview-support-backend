package com.abc.user_service.repository;

import com.abc.user_service.entity.EloHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EloHistoryRepository extends JpaRepository<EloHistory, Long> {
}