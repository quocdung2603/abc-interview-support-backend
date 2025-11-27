package com.abc.social_service.repository;

import com.abc.social_service.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    long countByCommentId(Long commentId);
    void deleteByCommentId(Long commentId);
}
