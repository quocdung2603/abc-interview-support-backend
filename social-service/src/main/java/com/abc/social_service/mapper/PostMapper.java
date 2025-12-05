package com.abc.social_service.mapper;

import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.PostRequest;
import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.entity.Post;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PostMapper {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    public Post toEntity(PostRequest request) {
        Post post = new Post();
        post.setFieldId(request.getFieldId());
        post.setTopicId(request.getTopicId());
        post.setLevelId(request.getLevelId());
        post.setPostType(request.getPostType());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setLockTime(request.getLockTime());
        return post;
    }
    
    public void updateEntityFromRequest(PostRequest request, Post post) {
        post.setFieldId(request.getFieldId());
        post.setTopicId(request.getTopicId());
        post.setLevelId(request.getLevelId());
        post.setPostType(request.getPostType());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setLockTime(request.getLockTime());
    }
    
    public PostResponse toResponse(Post post) {
        return toResponse(post, null, null, null);
    }
    
    public PostResponse toResponse(Post post, FieldResponse field, TopicResponse topic, LevelResponse level) {
        PostResponse response = new PostResponse();
        
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setFieldId(post.getFieldId());
        response.setTopicId(post.getTopicId());
        response.setLevelId(post.getLevelId());
        response.setPostType(post.getPostType());
        response.setStatus(post.getStatus());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        
        // Format dates to ISO 8601
        if (post.getLockTime() != null) {
            response.setLockTime(post.getLockTime().format(ISO_FORMATTER));
        }
        if (post.getCreatedAt() != null) {
            response.setCreatedAt(post.getCreatedAt().format(ISO_FORMATTER));
        }
        if (post.getUpdatedAt() != null) {
            response.setUpdatedAt(post.getUpdatedAt().format(ISO_FORMATTER));
        }
        
        // Add classification names if available
        if (field != null) {
            response.setFieldName(field.getName());
        }
        if (topic != null) {
            response.setTopicName(topic.getName());
        }
        if (level != null) {
            response.setLevelName(level.getName());
        }
        
        return response;
    }
}
