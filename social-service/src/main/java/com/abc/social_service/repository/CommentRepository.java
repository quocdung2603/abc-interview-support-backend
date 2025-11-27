package com.abc.social_service.repository;

import com.abc.social_service.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByPostIdOrderByVoteCountDescCreatedAtAsc(Long postId);
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<Comment> findByPostIdOrderByWeightedVoteScoreDescCreatedAtAsc(Long postId);
    Page<Comment> findByPostId(Long postId, Pageable pageable);
    long countByPostId(Long postId);
    long countByPostIdAndUserId(Long postId, Long userId);
}
