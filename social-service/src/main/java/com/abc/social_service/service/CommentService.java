package com.abc.social_service.service;

import com.abc.social_service.dto.CommentRequest;
import com.abc.social_service.dto.CommentResponse;
import com.abc.social_service.dto.CommentUpdateRequest;
import com.abc.social_service.entity.Comment;
import com.abc.social_service.entity.Post;
import com.abc.social_service.exception.CommentLimitExceededException;
import com.abc.social_service.exception.CommentNotFoundException;
import com.abc.social_service.exception.EditLimitExceededException;
import com.abc.social_service.exception.PostLockedException;
import com.abc.social_service.mapper.CommentMapper;
import com.abc.social_service.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final PostService postService;
    private final CommentLimitValidator commentLimitValidator;

    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        // Verify post exists and check if locked
        Post post = postService.getPostEntityById(request.getPostId());
        
        if (postService.isLocked(post)) {
            // Check if user can comment on locked post
            if (!commentLimitValidator.canComment(request.getPostId(), request.getUserId())) {
                log.warn("User {} attempted to comment multiple times on locked post {}", 
                        request.getUserId(), request.getPostId());
                throw new CommentLimitExceededException();
            }
        }
        
        Comment comment = commentMapper.toEntity(request);
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // Verify post exists
        Post post = postService.getPostEntityById(postId);
        
        List<Comment> comments;
        if (postService.isLocked(post)) {
            // Locked: sort by weighted vote score DESC, then created time ASC
            comments = commentRepository.findByPostIdOrderByWeightedVoteScoreDescCreatedAtAsc(postId);
        } else {
            // Not locked: sort by created time ASC
            comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        }
        
        return commentMapper.toResponseList(comments);
    }

    public Page<CommentResponse> getCommentsByPostIdPaginated(Long postId, Pageable pageable) {
        // Verify post exists
        postService.getPostEntityById(postId);
        
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        return commentMapper.toResponse(comment);
    }

    public Comment getCommentEntityById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException(id);
        }
        commentRepository.deleteById(id);
    }

    @Transactional
    public void incrementVoteCount(Long commentId) {
        Comment comment = getCommentEntityById(commentId);
        comment.setVoteCount(comment.getVoteCount() + 1);
        commentRepository.save(comment);
    }
    
    @Transactional
    public void updateWeightedVoteScore(Long commentId, double scoreChange) {
        Comment comment = getCommentEntityById(commentId);
        Double currentScore = comment.getWeightedVoteScore() != null ? comment.getWeightedVoteScore() : 0.0;
        comment.setWeightedVoteScore(currentScore + scoreChange);
        commentRepository.save(comment);
        log.debug("Updated weighted vote score for comment {} by {}, new score: {}", 
                commentId, scoreChange, comment.getWeightedVoteScore());
    }
    
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = getCommentEntityById(commentId);
        
        // Check ownership
        if (!comment.getUserId().equals(request.getUserId())) {
            throw new CommentNotFoundException(commentId);
        }
        
        // Check edit limit
        if (!commentLimitValidator.canEdit(commentId, request.getUserId())) {
            log.warn("User {} attempted to exceed edit limit for comment {}", 
                    request.getUserId(), commentId);
            throw new EditLimitExceededException();
        }
        
        // Update content and metadata
        comment.setContent(request.getContent());
        Integer currentEditCount = comment.getEditCount() != null ? comment.getEditCount() : 0;
        comment.setEditCount(currentEditCount + 1);
        comment.setUpdatedAt(LocalDateTime.now());
        
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment {} updated by user {}, edit count: {}", 
                commentId, request.getUserId(), updatedComment.getEditCount());
        
        return commentMapper.toResponse(updatedComment);
    }
}
