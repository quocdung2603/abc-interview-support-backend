package com.abc.user_service.controller;

import com.abc.user_service.dto.request.*;
import com.abc.user_service.dto.response.EloHistoryResponse;
import com.abc.user_service.dto.response.UserResponse;
import com.abc.user_service.entity.UserStatus;
import com.abc.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // Internal endpoints - called by Auth Service only
    @PostMapping("/internal/create")
    public UserResponse createInternal(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/internal/user/{id}")
    public UserResponse getUserInternal(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/check-email/{email}")
    public Boolean checkEmailExists(@PathVariable String email) {
        return userService.checkEmailExists(email);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        UserResponse response = userService.getByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-password")
    public Boolean validatePassword(@RequestBody Map<String, String> request) {
        return userService.validatePassword(request.get("email"), request.get("password"));
    }

    @PostMapping("/verify-token")
    public UserResponse verifyToken(@RequestBody Map<String, String> request) {
        return userService.verifyToken(request.get("token"));
    }

    @GetMapping("/{id:[0-9]+}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return userService.updateRole(id, request);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return userService.updateStatus(id, request);
    }

    @PostMapping("/elo")
    public UserResponse applyElo(@Valid @RequestBody EloApplyRequest request) {
        return userService.applyElo(request);
    }

    // Additional CRUD endpoints
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsersByRole(@PathVariable Long roleId, Pageable pageable) {
        return userService.getUsersByRole(roleId, pageable);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getUsersByStatus(@PathVariable UserStatus status, Pageable pageable) {
        return userService.getUsersByStatus(status, pageable);
    }

    @GetMapping("/roles")
    public java.util.List<com.abc.user_service.dto.response.RoleResponse> getAllRoles() {
        return userService.getAllRoles();
    }

    // Get user ELO rank (for internal service calls)
    @GetMapping("/{userId}/elo")
    public Map<String, Integer> getUserEloRank(@PathVariable Long userId) {
        UserResponse user = userService.getById(userId);
        Integer eloScore = user.getEloScore() != null ? user.getEloScore() : 1000;
        return Map.of("eloRank", eloScore);
    }

    // Elo History endpoints
    @GetMapping("/{userId}/elo-history")
    public Page<EloHistoryResponse> getEloHistory(
            @PathVariable Long userId,
            Pageable pageable) {
        return userService.getEloHistory(userId, pageable);
    }

    @GetMapping("/{userId}/elo-history/recent")
    public List<EloHistoryResponse> getRecentEloHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return userService.getRecentEloHistory(userId, limit);
    }

    @GetMapping("/{userId}/elo-history/all")
    public List<EloHistoryResponse> getAllEloHistory(@PathVariable Long userId) {
        return userService.getAllEloHistory(userId);
    }
}