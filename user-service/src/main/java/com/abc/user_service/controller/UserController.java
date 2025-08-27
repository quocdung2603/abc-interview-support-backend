package com.abc.user_service.controller;

import com.abc.user_service.dto.request.*;
import com.abc.user_service.dto.response.UserResponse;
import com.abc.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/verify")
    public UserResponse verify(@RequestParam("token") String token) {
        return userService.verify(token);
    }

    @PutMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return userService.updateRole(id, request);
    }

    @PutMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return userService.updateStatus(id, request);
    }

    @PostMapping("/elo")
    public UserResponse applyElo(@Valid @RequestBody EloApplyRequest request) {
        return userService.applyElo(request);
    }
}