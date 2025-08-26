package com.abc.user_service.dto.request;


import com.abc.user_service.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusUpdateRequest {
    @NotNull
    private UserStatus status;
}