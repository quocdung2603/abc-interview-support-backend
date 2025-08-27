package com.auth.service.controller;

import com.auth.service.dto.LoginRequest;
import com.auth.service.dto.*;
import com.auth.service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto user = authService.register(request);  // gọi user-service tạo user
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);     // gọi user-service /users/login
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<TokenResponse> verify(@RequestParam("token") String token) {
        return ResponseEntity.ok(authService.verify(token));
    }

    // ví dụ: gọi user-service lấy user theo id
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getUserById(id));
    }
}