package com.abc.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EloApplyRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String action;
    @NotNull
    private Integer points;
    private String description;
}