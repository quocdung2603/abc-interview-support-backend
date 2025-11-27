package com.abc.social_service.service;

import com.abc.social_service.entity.Comment;
import com.abc.social_service.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for validating comment and edit limits on locked posts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLimitValidator {
    
    private final CommentRepository commentRepository;
    private static final int MAX_EDITS_PER_COMMENT = 1;
    
    /**
     * Checks if user can comment on a locked post
     * @param postId The post ID
     * @param userId The user ID
     * @return true if user hasn't commented yet
     */
    public boolean canComment(Long postId, Long userId) {
        long commentCount = commentRepository.countByPostIdAndUserId(postId, userId);
        boolean canComment = commentCount == 0;
        
        log.debug("User {} has {} comments on post {}. Can comment: {}", 
                userId, commentCount, postId, canComment);
        
        return canComment;
    }
    
    /**
     * Checks if user can edit their comment
     * @param commentId The comment ID
     * @param userId The user ID
     * @return true if user owns comment and hasn't exceeded edit limit
     */
    public boolean canEdit(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        
        if (comment == null) {
            log.warn("Comment {} not found", commentId);
            return false;
        }
        
        if (!comment.getUserId().equals(userId)) {
            log.warn("User {} does not own comment {}", userId, commentId);
            return false;
        }
        
        Integer editCount = comment.getEditCount() != null ? comment.getEditCount() : 0;
        boolean canEdit = editCount < MAX_EDITS_PER_COMMENT;
        
        log.debug("Comment {} has {} edits. Can edit: {}", commentId, editCount, canEdit);
        
        return canEdit;
    }
}
