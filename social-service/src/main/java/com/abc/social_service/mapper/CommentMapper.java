package com.abc.social_service.mapper;

import com.abc.social_service.dto.CommentRequest;
import com.abc.social_service.dto.CommentResponse;
import com.abc.social_service.entity.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toEntity(CommentRequest request);
    
    default CommentResponse toResponse(Comment comment) {
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
        response.setEditCount(comment.getEditCount());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        
        return response;
    }
    
    List<CommentResponse> toResponseList(List<Comment> comments);
}
