package com.abc.social_service.repository;

import com.abc.social_service.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Find posts by status
    Page<Post> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    // Find PUBLISHED posts OR user's own DRAFT posts
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' OR (p.status = 'DRAFT' AND p.userId = :userId) ORDER BY p.createdAt DESC")
    Page<Post> findPublishedOrOwnDrafts(@Param("userId") Long userId, Pageable pageable);
    
    // Find posts by status and user ID
    Page<Post> findByStatusAndUserIdOrderByCreatedAtDesc(String status, Long userId, Pageable pageable);
    
    // Classification filtering methods
    Page<Post> findByFieldId(Long fieldId, Pageable pageable);
    Page<Post> findByTopicId(Long topicId, Pageable pageable);
    Page<Post> findByLevelId(Long levelId, Pageable pageable);
    Page<Post> findByFieldIdAndTopicId(Long fieldId, Long topicId, Pageable pageable);
    Page<Post> findByFieldIdAndTopicIdAndLevelId(Long fieldId, Long topicId, Long levelId, Pageable pageable);
    Page<Post> findByFieldIdAndLevelId(Long fieldId, Long levelId, Pageable pageable);
    Page<Post> findByTopicIdAndLevelId(Long topicId, Long levelId, Pageable pageable);
}
