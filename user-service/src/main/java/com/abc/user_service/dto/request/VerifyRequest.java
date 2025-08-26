package com.abc.user_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyRequest {
    @NotNull
    private Long userId;
}