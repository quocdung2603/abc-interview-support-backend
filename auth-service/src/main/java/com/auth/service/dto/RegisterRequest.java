package com.auth.service.dto;

import lombok.Data;

import java.time.LocalDate;
@Data
public class RegisterRequest {
    private Long roleId;
    private String email;
    private String password;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isStudying;
}
