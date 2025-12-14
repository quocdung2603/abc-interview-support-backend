package com.abc.social_service.mapper;

import com.abc.social_service.dto.CommentRequest;
import com.abc.social_service.dto.CommentResponse;
import com.abc.social_service.entity.Comment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
    
    public Comment toEntity(CommentRequest request) {
        if (request == null) {
            return null;
        }
        
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        
        return comment;
    }
    
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPostId());
        response.setUserId(comment.getUserId());
        response.setContent(comment.getContent());
        response.setVoteCount(comment.getVoteCount());
        response.setWeightedVoteScore(comment.getWeightedVoteScore());
        response.setVotePercentage(comment.getVotePercentage());
        response.setUsefulVoteCount(comment.getUsefulVoteCount());
        response.setNotUsefulVoteCount(comment.getNotUsefulVoteCount());
        response.setUsefulPercentage(comment.getUsefulPercentage());
        response.setNotUsefulPercentage(comment.getNotUsefulPercentage());
        response.setEditCount(comment.getEditCount());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        
        return response;
    }
    
    public List<CommentResponse> toResponseList(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
