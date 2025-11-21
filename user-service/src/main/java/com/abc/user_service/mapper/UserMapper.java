package com.abc.user_service.mapper;

import com.abc.user_service.dto.request.UserRequest;
import com.abc.user_service.dto.response.EloHistoryResponse;
import com.abc.user_service.dto.response.UserResponse;
import com.abc.user_service.entity.EloHistory;
import com.abc.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role.id", source = "roleId")
    @Mapping(target = "password", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "roleName", expression = "java(user.getRole() != null && user.getRole().getRoleName() != null ? user.getRole().getRoleName().name() : null)")
    UserResponse toResponse(User user);
    
    @Mapping(target = "userId", source = "user.id")
    EloHistoryResponse toEloHistoryResponse(EloHistory eloHistory);
}