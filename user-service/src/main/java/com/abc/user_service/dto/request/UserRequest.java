package com.abc.user_service.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequest {
    private Long roleId;
    private String email;
    private String password;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isStudying;
}