package com.abc.user_service.mapper;

import com.abc.user_service.dto.request.UserRequest;
import com.abc.user_service.dto.response.UserResponse;
import com.abc.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role.id", source = "roleId")
    User toEntity(UserRequest request);

    @Mapping(target = "roleId", source = "role.id")
    UserResponse toResponse(User user);
}