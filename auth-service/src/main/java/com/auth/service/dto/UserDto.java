package com.auth.service.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private Long roleId;
    private String email;
    private String fullName;
}