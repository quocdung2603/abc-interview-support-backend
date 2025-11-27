package com.abc.social_service.mapper;

import com.abc.social_service.dto.PostRequest;
import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {
    Post toEntity(PostRequest request);
    PostResponse toResponse(Post post);
    List<PostResponse> toResponseList(List<Post> posts);
    void updateEntityFromRequest(PostRequest request, @MappingTarget Post post);
}
